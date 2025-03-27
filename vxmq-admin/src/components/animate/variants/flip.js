import { transitionExit, transitionEnter } from './transition';

// ----------------------------------------------------------------------

export const varFlip = (direction, options) => {
  const transitionIn = options?.transitionIn;
  const transitionOut = options?.transitionOut;

  const variants = {
    /**** In ****/
    inX: {
      initial: { rotateX: -180, opacity: 0 },
      animate: {
        rotateX: 0,
        opacity: 1,
        transition: transitionEnter(transitionIn),
      },
      exit: {
        rotateX: -180,
        opacity: 0,
        transition: transitionExit(transitionOut),
      },
    },
    inY: {
      initial: { rotateY: -180, opacity: 0 },
      animate: {
        rotateY: 0,
        opacity: 1,
        transition: transitionEnter(transitionIn),
      },
      exit: {
        rotateY: -180,
        opacity: 0,
        transition: transitionExit(transitionOut),
      },
    },
    /**** Out ****/
    outX: {
      initial: { rotateX: 0, opacity: 1 },
      animate: {
        rotateX: 70,
        opacity: 0,
        transition: transitionExit(transitionOut),
      },
    },
    outY: {
      initial: { rotateY: 0, opacity: 1 },
      animate: {
        rotateY: 70,
        opacity: 0,
        transition: transitionExit(transitionOut),
      },
    },
  };

  return variants[direction];
};
