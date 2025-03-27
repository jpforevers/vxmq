import { forwardRef } from 'react';
import { mergeClasses } from 'minimal-shared/utils';

import { styled } from '@mui/material/styles';

import { flagIconClasses } from './classes';

// ----------------------------------------------------------------------

export const FlagIcon = forwardRef((props, ref) => {
  const { code, className, sx, ...other } = props;

  if (!code) {
    return null;
  }

  return (
    <FlagRoot
      ref={ref}
      className={mergeClasses([flagIconClasses.root, className])}
      sx={sx}
      {...other}
    >
      <FlagImg
        loading="lazy"
        alt={code}
        src={`https://purecatamphetamine.github.io/country-flag-icons/3x2/${code?.toUpperCase()}.svg`}
        className={flagIconClasses.img}
      />
    </FlagRoot>
  );
});

// ----------------------------------------------------------------------

const FlagRoot = styled('span')(({ theme }) => ({
  width: 26,
  height: 20,
  flexShrink: 0,
  overflow: 'hidden',
  borderRadius: '5px',
  alignItems: 'center',
  display: 'inline-flex',
  justifyContent: 'center',
  backgroundColor: theme.vars.palette.background.neutral,
}));

const FlagImg = styled('img')(() => ({
  width: '100%',
  height: '100%',
  maxWidth: 'unset',
  objectFit: 'cover',
}));
