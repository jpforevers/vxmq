import { transitionExit, transitionEnter } from './transition';

// ----------------------------------------------------------------------

export const varSlide = (direction, options) => {
  const distance = options?.distance || 160;
  const transitionIn = options?.transitionIn;
  const transitionOut = options?.transitionOut;

  const variants = {
    /**** In ****/
    inUp: {
      initial: { y: distance },
      animate: { y: 0, transition: transitionEnter(transitionIn) },
      exit: { y: distance, transition: transitionExit(transitionOut) },
    },
    inDown: {
      initial: { y: -distance },
      animate: { y: 0, transition: transitionEnter(transitionIn) },
      exit: { y: -distance, transition: transitionExit(transitionOut) },
    },
    inLeft: {
      initial: { x: -distance },
      animate: { x: 0, transition: transitionEnter(transitionIn) },
      exit: { x: -distance, transition: transitionExit(transitionOut) },
    },
    inRight: {
      initial: { x: distance },
      animate: { x: 0, transition: transitionEnter(transitionIn) },
      exit: { x: distance, transition: transitionExit(transitionOut) },
    },
    /**** Out ****/
    outUp: {
      initial: { y: 0 },
      animate: { y: -distance, transition: transitionEnter(transitionIn) },
      exit: { y: 0, transition: transitionExit(transitionOut) },
    },
    outDown: {
      initial: { y: 0 },
      animate: { y: distance, transition: transitionEnter(transitionIn) },
      exit: { y: 0, transition: transitionExit(transitionOut) },
    },
    outLeft: {
      initial: { x: 0 },
      animate: { x: -distance, transition: transitionEnter(transitionIn) },
      exit: { x: 0, transition: transitionExit(transitionOut) },
    },
    outRight: {
      initial: { x: 0 },
      animate: { x: distance, transition: transitionEnter(transitionIn) },
      exit: { x: 0, transition: transitionExit(transitionOut) },
    },
  };

  return variants[direction];
};
