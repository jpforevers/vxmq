import { Link } from 'react-router';

// ----------------------------------------------------------------------

export function RouterLink({ href, ref, ...other }) {
  return <Link ref={ref} to={href} {...other} />;
}
