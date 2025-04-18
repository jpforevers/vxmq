import { transitionExit, transitionEnter } from './transition';

// ----------------------------------------------------------------------

export const varZoom = (direction, options) => {
  const distance = options?.distance || 720;
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
    inUp: {
      initial: {
        scale: 0,
        opacity: 0,
        translateY: distance,
      },
      animate: {
        scale: 1,
        opacity: 1,
        translateY: 0,
        transition: transitionEnter(transitionIn),
      },
      exit: {
        scale: 0,
        opacity: 0,
        translateY: distance,
        transition: transitionExit(transitionOut),
      },
    },
    inDown: {
      initial: { scale: 0, opacity: 0, translateY: -distance },
      animate: {
        scale: 1,
        opacity: 1,
        translateY: 0,
        transition: transitionEnter(transitionIn),
      },
      exit: {
        scale: 0,
        opacity: 0,
        translateY: -distance,
        transition: transitionExit(transitionOut),
      },
    },
    inLeft: {
      initial: { scale: 0, opacity: 0, translateX: -distance },
      animate: {
        scale: 1,
        opacity: 1,
        translateX: 0,
        transition: transitionEnter(transitionIn),
      },
      exit: {
        scale: 0,
        opacity: 0,
        translateX: -distance,
        transition: transitionExit(transitionOut),
      },
    },
    inRight: {
      initial: { scale: 0, opacity: 0, translateX: distance },
      animate: {
        scale: 1,
        opacity: 1,
        translateX: 0,
        transition: transitionEnter(transitionIn),
      },
      exit: {
        scale: 0,
        opacity: 0,
        translateX: distance,
        transition: transitionExit(transitionOut),
      },
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
    outUp: {
      initial: { scale: 1, opacity: 1 },
      animate: {
        scale: 0,
        opacity: 0,
        translateY: -distance,
        transition: transitionEnter(transitionIn),
      },
    },
    outDown: {
      initial: { scale: 1, opacity: 1 },
      animate: {
        scale: 0,
        opacity: 0,
        translateY: distance,
        transition: transitionEnter(transitionIn),
      },
    },
    outLeft: {
      initial: { scale: 1, opacity: 1 },
      animate: {
        scale: 0,
        opacity: 0,
        translateX: -distance,
        transition: transitionEnter(transitionIn),
      },
    },
    outRight: {
      initial: { scale: 1, opacity: 1 },
      animate: {
        scale: 0,
        opacity: 0,
        translateX: distance,
        transition: transitionEnter(transitionIn),
      },
    },
  };

  return variants[direction];
};
