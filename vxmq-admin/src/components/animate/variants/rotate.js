import { transitionExit, transitionEnter } from './transition';

// ----------------------------------------------------------------------

export const varRotate = (direction, options) => {
  const deg = options?.deg || 360;
  const transitionIn = options?.transitionIn;
  const transitionOut = options?.transitionOut;

  const variants = {
    /**** In ****/
    in: {
      initial: { opacity: 0, rotate: -deg },
      animate: { opacity: 1, rotate: 0, transition: transitionEnter(transitionIn) },
      exit: { opacity: 0, rotate: -deg, transition: transitionExit(transitionOut) },
    },
    /**** Out ****/
    out: {
      initial: { opacity: 1, rotate: 0 },
      animate: { opacity: 0, rotate: -deg, transition: transitionExit(transitionOut) },
    },
  };

  return variants[direction];
};
