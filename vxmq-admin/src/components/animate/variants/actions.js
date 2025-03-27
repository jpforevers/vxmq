// ----------------------------------------------------------------------

export const varHover = (value = 1.09) => ({
  scale: value,
});

export const varTap = (value = 0.9) => ({
  scale: value,
});

export const transitionTap = (props) => ({
  type: 'spring',
  stiffness: 400,
  damping: 18,
  ...props,
});

export const transitionHover = (props) => ({
  duration: 0.32,
  ease: [0.43, 0.13, 0.23, 0.96],
  ...props,
});
