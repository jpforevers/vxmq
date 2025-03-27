import { forwardRef } from 'react';
import { Link } from 'react-router';

export const RouterLink = forwardRef(({ href, ...other }, ref) => (
  <Link ref={ref} to={href} {...other} />
));
