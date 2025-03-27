// ----------------------------------------------------------------------

export const varContainer = (props) => ({
  animate: {
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0.05,
      ...props?.transitionIn,
    },
  },
  exit: {
    transition: {
      staggerChildren: 0.05,
      staggerDirection: -1,
      ...props?.transitionOut,
    },
  },
});
