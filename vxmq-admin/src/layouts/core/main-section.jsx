import { mergeClasses } from 'minimal-shared/utils';

import { styled } from '@mui/material/styles';

import { layoutClasses } from '../core/classes';

// ----------------------------------------------------------------------

export function MainSection({ children, className, sx, ...other }) {
  return (
    <MainRoot className={mergeClasses([layoutClasses.main, className])} sx={sx} {...other}>
      {children}
    </MainRoot>
  );
}

// ----------------------------------------------------------------------

const MainRoot = styled('main')({
  display: 'flex',
  flex: '1 1 auto',
  flexDirection: 'column',
});
