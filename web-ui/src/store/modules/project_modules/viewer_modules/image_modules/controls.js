import constants from '@/utils/constants';

export default {
  state() {
    return {
      step: {
        channel: constants.DEFAULT_IMAGE_CONTROLS_STEP,
        zStack: constants.DEFAULT_IMAGE_CONTROLS_STEP,
        time: constants.DEFAULT_IMAGE_CONTROLS_STEP,
      }
    };
  },

  mutations: {
    setStep(state, {dimension, value}) {
      state.step[dimension] = value;
    }
  },
};
