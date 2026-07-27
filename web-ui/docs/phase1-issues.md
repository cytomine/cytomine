# Phase 1: Vue 2 idiom removal

Phase 1 of the web-ui Vue 2 → Vue 3 migration removes Vue-2-only idioms that
either have no Vue 3 equivalent or would otherwise block the `@vue/compat`
switch in Phase 3. Each item below is written up as a standalone,
GitHub-issue-ready task: mechanical where possible, decision-flagged where not.

Prior phases: Phase 0 (Vue CLI → Vite) and the Jest → Vitest migration are
done. Phase 2 (Buefy → `@ntohq/buefy-next`, viewer/vuelayers rewrite,
chart.js upgrade) and Phase 3 (`@vue/compat` switch, `@vue/test-utils` v1 → v2)
follow this one.

Template filters (Issue 2, issue #1101, [#1104](../../../pull/1104)), deep
selectors (Issue 4, issue #244, [#1109](../../../pull/1109)), and `$eventBus`
(Issue 1, issue #1110, branch `1110-eventbus-native-eventtarget`, PR pending)
are all shipped or implemented — dropped from this list. `vee-validate`
(Issue 5, no GitHub issue filed yet) and `.sync` (Issue 3, issue #1102) are
deliberately *not* part of Phase 1 — see
[Deferred to Phase 3](#deferred-to-phase-3) below for why.

That's every originally-scoped Phase 1 issue accounted for (done, or
deliberately deferred). Nothing left open in Phase 1 itself; Phase 2
(Buefy/viewer/chart.js) is next.

---

## Issue 1: Replace `$eventBus` with a native `EventTarget` — DONE (2026-07-24)

Implemented on branch `1110-eventbus-native-eventtarget` (issue #1110), PR pending.

26 files / ~100 call sites (14 distinct event names) converted from
`this.$eventBus.$on/$off/$emit` to an imported `eventBus` singleton
(`src/utils/event-bus.js`) wrapping native `EventTarget`; `Vue.prototype.$eventBus`
removed from `src/main.js`. `mitt` was considered and rejected (last real
release 2023).

The shipped design differs from the original proposal above in two ways,
both found by actually surveying every call site rather than assuming a
single-value `detail`:

- **`emit(type, ...args)`** takes any number of positional args (stored as an
  array in `detail`), and `on()` wraps each listener so it's invoked with
  `listener(...event.detail)` — so handler signatures needed **zero changes**.
  This mattered because one real call site
  (`DrawTools.vue`: `$eventBus.$emit('addAnnotation', annot, false)`) passes
  two positional arguments, not one; a naive single-`detail` design would have
  silently dropped the second (`LayersPanel.vue`'s `addAnnotationEventHandler(annot, saved = true)`).
- **One call site used Vue 2's no-listener `$off(type)` overload**
  (`CytomineImage.vue`: `$eventBus.$on('close-metadata', () => ...)` paired
  with `$eventBus.$off('close-metadata')`, an inline arrow with nothing to
  match on removal) — native `removeEventListener` has no "remove all
  listeners for this type" equivalent, so this one spot was converted to a
  named `closeMetadataHandler()` method instead, matching the pattern already
  used everywhere else in that file.

8 test files had stale `$eventBus` test doubles; 6 were dead no-op mocks
(components no longer read `this.$eventBus` at all) and were deleted, 2
(`AppBottomDrawer.test.js`, `MetadataPanel.test.js`) asserted on
`wrapper.vm.$eventBus.$emit` and were rewritten to `vi.mock('@/utils/event-bus', ...)`
+ assert on the mocked `eventBus.emit`. Build, lint, and full 352-test suite
verified green after.

---

## Deferred to Phase 3

### `.sync` → `v-model:prop` (originally Issue 3)

**Status:** deliberately out of Phase 1 — cannot be done correctly before the Vue 3 switch.

This was originally scoped as a Phase 1 mechanical rename (34 files, 80 call
sites: Buefy's paginated-table props — `current-page.sync`, `per-page.sync`,
`sort.sync`, `order.sync`, `opened-detailed.sync`, `checked-rows.sync` — across
most list views, plus one-off `active.sync` modal-visibility bindings). It
turned out **not** to be safe to do ahead of time: attempted on branch
`1102-sync-modifier` and reverted after verifying against the actual installed
`vue-template-compiler@2.7.16` (used via `@vitejs/plugin-vue2`) that Vue 2.7's
compiler **silently ignores the argument on `v-model:prop`** for components —
it always compiles to a single `model: {value, callback}` bound to the generic
`value` prop / `input` event, same as bare `v-model`. Worse, when a component
has several `v-model:x` attributes (as every paginated-table usage does), only
the *last* one compiles to anything; the rest are silently dropped with no
compiler warning:

```js
// input: v-model:currentPage, v-model:perPage, v-model:sort, v-model:order
// output — only `order`'s value survives, bound to the wrong prop/event:
_c('cytomine-table',{model:{value:(sortOrder),callback:function ($$v) {sortOrder=$$v}, ...}})
```

So `.sync` must stay as-is in Vue 2.7 and only convert to `v-model:prop` once
the compiler is Vue 3's (Phase 3). Re-verify this doesn't regress if the Vite
toolchain or `vue-template-compiler` version changes before then.

**Acceptance criteria (Phase 3, not Phase 1):**
- [ ] All `.sync` usages across the 34 files replaced with `v-model:prop`
      equivalents, once running on Vue 3's compiler.
- [ ] `grep -rE '\.sync='` returns no results under `src/`.
- [ ] Any in-house (non-Buefy) child relying on `.sync` verified to already
      emit `update:prop`, or updated to do so.
- [ ] Build, full test suite, and lint pass.

---

### `vee-validate` (originally Issue 5)

**Status:** deliberately out of Phase 1 — bundled into the Phase 3 Vue 3 cutover instead.

#### Why this isn't a Phase 1 issue

`vee-validate` v2.2.4 is registered globally in `src/main.js`:

```js
import VeeValidate, {Validator} from 'vee-validate';
Validator.extend('positive', value => Number(value) > 0);
Vue.use(VeeValidate, {i18nRootKey: 'validations', i18n, inject: false});
```

Unlike Issues 1/3/4, there's no mechanical Vue-2-idiom swap here: v2's
directive/global-instance API (`v-validate`, `this.$validator`,
`errors.has()`/`errors.first()`) has no equivalent that runs on both Vue 2 and
Vue 3, so any replacement requires rewriting all 14 validated forms regardless
of target. Since the rewrite happens exactly once no matter when it's
scheduled, there's no benefit to doing it ahead of the Vue 3 switch — better
to target the final API directly at cutover time than to land an intermediate
rewrite (e.g. vee-validate v3) now and a second one later.

#### Scope (unchanged from original survey)

14 files use `v-validate` + `errors.has()`/`errors.first()`. Custom rule in use:
`positive` (`Validator.extend('positive', value => Number(value) > 0)`).
Built-in rules in use across those files include at least `required`, `decimal`,
and the custom `positive` (seen in `CalibrationModal.vue`,
`MagnificationModal.vue`). No `ValidationProvider`/`ValidationObserver`
component usage exists yet (0 hits), so there's no partial-v3/v4-style usage
to reconcile.

#### Target: `@tanstack/vue-form`, replacing vee-validate v2 directly

Skips v3 and v4 as intermediate steps. `@tanstack/vue-form` peer-depends on
`vue: ^3.4.0` (Composition-API-shaped: `useForm()` + `<form.Field>`), so it's
unusable before the Vue 3 switch — same constraint vee-validate v4 has. No
built-in rule DSL (unlike vee-validate's `'required|decimal|positive'`
strings); validation is bring-your-own (zod/yup/custom), so the `positive`
rule and friends get rebuilt as plain functions rather than carried over.

#### Risk to verify before relying on this sequencing

vee-validate v2 declares no explicit Vue peer dependency, so nothing blocks
installing it alongside Vue 3, but its directive/instance API was built
against Vue 2 internals and was never tested under `@vue/compat`. **Before
committing to "defer everything to cutover time,"** spike-test whether
`v-validate`/`errors.has()` still function once `vue` is bumped to 3.x in
compat mode:
- If it survives under `@vue/compat`: the TanStack Form rewrite can genuinely
  happen any time after Phase 3 ships, at its own pace.
- If it breaks: the rewrite of all 14 forms must land in the *same* PR/branch
  as the Vue 3 version bump, since the forms would otherwise be non-functional
  the moment that PR merges.

#### Acceptance criteria (Phase 3, not Phase 1)

- [ ] Spike: confirm whether vee-validate v2 keeps working under `@vue/compat`,
      and sequence the rewrite accordingly (see above).
- [ ] All 14 files rewritten from `v-validate`/`errors.has()` to
      `@tanstack/vue-form`'s `useForm()`/`<form.Field>`, including a
      hand-written replacement for the `positive` custom rule.
- [ ] Build, full test suite, and lint pass.
