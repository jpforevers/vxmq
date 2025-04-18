import { domMax, LazyMotion } from 'framer-motion';

// ----------------------------------------------------------------------

export function MotionLazy({ children }) {
  return (
    <LazyMotion strict features={domMax}>
      {children}
    </LazyMotion>
  );
}
