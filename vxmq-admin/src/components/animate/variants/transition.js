// ----------------------------------------------------------------------

export const transitionEnter = (props) => ({
  duration: 0.64,
  ease: [0.43, 0.13, 0.23, 0.96],
  ...props,
});

export const transitionExit = (props) => ({
  duration: 0.48,
  ease: [0.43, 0.13, 0.23, 0.96],
  ...props,
});
