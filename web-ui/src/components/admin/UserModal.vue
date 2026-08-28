<template>
<form @submit.prevent="save()">
  <cytomine-modal :active="active" :title="title" @close="$emit('update:active', false)">
    <!-- HACK: fake fields to prevent autofill -->
    <input id="username" class="hidden" type="text">
    <input id="password" class="hidden" type="password">

    <field :form="form" name="username" :validators="requiredRule" v-slot="{field, state}">
      <b-field
        :label="$t('username')"
        horizontal
        :type="{'is-danger': !!state.meta.errors.length}"
        :message="state.meta.errors[0]"
      >
        <b-input
          name="username"
          :disabled="editionMode"
          :model-value="state.value"
          :type="'text'"
          @update:model-value="field.handleChange"
        />
      </b-field>
    </field>

    <field
      v-for="{name, validators} in editableFields"
      :key="name"
      :form="form"
      :name="name"
      :validators="validators"
      v-slot="{field, state}"
    >
      <b-field
        :label="$t(name === 'password' && editionMode ? 'password-new' : name)"
        horizontal
        :type="{'is-danger': !!state.meta.errors.length}"
        :message="state.meta.errors[0]"
      >
        <b-input
          :model-value="state.value"
          :name="name"
          :password-reveal="name === 'password'"
          :type="name === 'password' ? 'password': 'text'"
          @update:model-value="field.handleChange"
        />
      </b-field>
    </field>

    <b-field :label="$t('role')" horizontal>
      <b-select v-model="selectedRole">
        <option v-for="(value, key) in roles" :value="key" :key="key">
          {{$t(value.label)}}
        </option>
      </b-select>
    </b-field>

    <b-field horizontal v-if="isChangingRoleToAdmin()">
      <b-checkbox v-model="adminConfirm">
        {{$t('admin-warning')}}
      </b-checkbox>
    </b-field>

    <b-field :label="$t('language')" horizontal>
      <b-select v-model="internalUser['language']">
        <option v-for="{value, name} in languages" :key="value" :value="value">
          {{name}}
        </option>
      </b-select>
    </b-field>

    <b-field :label="$t('developer-mode')" horizontal>
      <b-switch v-model="internalUser.isDeveloper" class="switch">
        <template v-if="internalUser.isDeveloper">{{$t('yes')}}</template>
        <template v-else>{{$t('no')}}</template>
      </b-switch>
    </b-field>

    <template #footer>
      <button class="button" type="button" @click="$emit('update:active', false)">
        {{$t('button-cancel')}}
      </button>
      <button class="button is-link" :disabled="!isValid || !isAdminConfirmed()">
        {{$t('button-save')}}
      </button>
    </template>
  </cytomine-modal>
</form>
</template>

<script>
import { Field, useForm } from '@tanstack/vue-form';

import { User } from '@/api';
import { email, min, required, rules, validateForm } from '@/utils/form.js';
import { rolesMapping } from '@/utils/role-utils';
import { UserRole } from '@/constants/UserRole.js';
import CytomineModal from '@/components/utils/CytomineModal.vue';

const emptyUser = { username: '', firstname: '', lastname: '', email: '', password: '' };

const defaultRole = UserRole.GUEST;
const defaultLanguage = { value: 'EN', name:'English' };

export default {
  name: 'user-modal',
  props: {
    active: Boolean,
    user: Object
  },
  components: { CytomineModal, Field },
  setup() {
    const form = useForm({ defaultValues: { ...emptyUser } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      requiredRule: { onChange: rules(required) }
    };
  },
  data() {
    return {
      internalUser: {},
      selectedRole: defaultRole,
      displayErrors: false,
      adminConfirm: false,
      languages: [
        { value: 'EN', name:'English' },
        { value: 'FR', name:'Français' },
        { value: 'ES', name:'Español' },
        { value: 'NL', name:'Nederlands' }
      ]
    };
  },
  computed: {
    roles() {
      return rolesMapping;
    },
    editionMode() {
      return Boolean(this.user);
    },
    title() {
      return this.$t(this.editionMode ? 'update-user' : 'create-user');
    },
    editableFields() {
      // An existing user keeps their password unless one is typed, so `required`
      // only applies on creation.
      return [
        { name: 'firstname', validators: this.requiredRule },
        { name: 'lastname', validators: this.requiredRule },
        { name: 'email', validators: { onChange: rules(required, email) } },
        {
          name: 'password',
          validators: {
            onChange: this.editionMode ? rules(min(8)) : rules(required, min(8))
          }
        }
      ];
    },
  },
  watch: {
    selectedRole() {
      this.adminConfirm = !this.isChangingRoleToAdmin();
    },
    active(val) {
      if (val) {
        this.internalUser = this.user ? this.user.clone() : new User();
        this.selectedRole = this.user ? this.user.role : defaultRole;
        this.internalUser.language = this.user ? this.user.language : defaultLanguage.value;
        this.displayErrors = false;
        this.adminConfirm = false;
        this.form.reset({
          ...emptyUser,
          ...(this.user ? {
            username: this.user.username,
            firstname: this.user.firstname,
            lastname: this.user.lastname,
            email: this.user.email
          } : {})
        });
      }
    }
  },
  methods: {
    isChangingRoleToAdmin() {
      let currentRole = this.user ? this.user.role : defaultRole;
      return this.isNotAdmin(currentRole) && !this.isNotAdmin(this.selectedRole);
    },
    isNotAdmin(role) {
      return role !== UserRole.ADMIN && role !== UserRole.SUPER_ADMIN;
    },
    isAdminConfirmed() {
      return this.adminConfirm || !this.isChangingRoleToAdmin();
    },

    async save() {
      if (!await validateForm(this.form)) {
        return;
      }

      let { password, ...profile } = this.form.state.values;
      let labelTranslation = this.editionMode ? 'update' : 'creation';
      Object.assign(this.internalUser, profile);
      // On update an empty password means "leave it alone", so it is only
      // carried over when one was actually typed.
      if (password) {
        this.internalUser.password = password;
      }
      this.internalUser.name = `${profile.firstname} ${profile.lastname}`;
      this.internalUser.reference = crypto.randomUUID();
      this.internalUser.role = this.selectedRole;
      try {
        await this.internalUser.save();
        if (!this.editionMode || this.selectedRole !== this.user.role) {
          await this.internalUser.defineRole(this.selectedRole);
          this.internalUser.role = this.selectedRole; // for correct rendering in list
        }

        this.form.setFieldValue('password', ''); // so that if modal reopened, field empty
        this.$notify({ type: 'success', text: this.$t('notif-success-user-' + labelTranslation) });
        this.$emit('update:active', false);
        this.$emit(this.editionMode ? 'updateUser' : 'addUser', this.internalUser);
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-user-' + labelTranslation) });
      }
    },
  },
};
</script>

<style scoped>
.hidden {
  display: none;
}

:deep(.modal-card), :deep(.modal-card-body) {
  width: 100vw;
  max-width: 800px;
}
</style>
