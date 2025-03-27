import { transitionExit, transitionEnter } from './transition';

// ----------------------------------------------------------------------

export const varScale = (direction, options) => {
  const transitionIn = options?.transitionIn;
  const transitionOut = options?.transitionOut;

  const variants = {
    /**** In ****/
    in: {
      initial: { scale: 0, opacity: 0 },
      animate: {
        scale: 1,
        opacity: 1,
        transition: transitionEnter(transitionIn),
      },
      exit: { scale: 0, opacity: 0, transition: transitionExit(transitionOut) },
    },
    inX: {
      initial: { scaleX: 0, opacity: 0 },
      animate: {
        scaleX: 1,
        opacity: 1,
        transition: transitionEnter(transitionIn),
      },
      exit: { scaleX: 0, opacity: 0, transition: transitionExit(transitionOut) },
    },
    inY: {
      initial: { scaleY: 0, opacity: 0 },
      animate: {
        scaleY: 1,
        opacity: 1,
        transition: transitionEnter(transitionIn),
      },
      exit: { scaleY: 0, opacity: 0, transition: transitionExit(transitionOut) },
    },
    /**** Out ****/
    out: {
      initial: { scale: 1, opacity: 1 },
      animate: {
        scale: 0,
        opacity: 0,
        transition: transitionEnter(transitionIn),
      },
    },
    outX: {
      initial: { scaleX: 1, opacity: 1 },
      animate: {
        scaleX: 0,
        opacity: 0,
        transition: transitionEnter(transitionIn),
      },
    },
    outY: {
      initial: { scaleY: 1, opacity: 1 },
      animate: {
        scaleY: 0,
        opacity: 0,
        transition: transitionEnter(transitionIn),
      },
    },
  };

  return variants[direction];
};
