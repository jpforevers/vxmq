import { upperFirst } from 'es-toolkit';
import { mergeClasses } from 'minimal-shared/utils';

import { labelClasses } from './classes';
import { LabelRoot, LabelIcon } from './styles';

// ----------------------------------------------------------------------

export function Label({
  sx,
  endIcon,
  children,
  startIcon,
  className,
  disabled,
  variant = 'soft',
  color = 'default',
  ...other
}) {
  return (
    <LabelRoot
      color={color}
      variant={variant}
      disabled={disabled}
      className={mergeClasses([labelClasses.root, className])}
      sx={sx}
      {...other}
    >
      {startIcon && <LabelIcon className={labelClasses.icon}>{startIcon}</LabelIcon>}

      {typeof children === 'string' ? upperFirst(children) : children}

      {endIcon && <LabelIcon className={labelClasses.icon}>{endIcon}</LabelIcon>}
    </LabelRoot>
  );
}
