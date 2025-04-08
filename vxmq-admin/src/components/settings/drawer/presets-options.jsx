import Box from '@mui/material/Box';
import { alpha as hexAlpha } from '@mui/material/styles';

import { OptionButton } from './styles';

// ----------------------------------------------------------------------

export function PresetsOptions({ sx, icon, value, options, onChangeOption, ...other }) {
  return (
    <Box
      sx={[
        {
          gap: 1.5,
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
        },
        ...(Array.isArray(sx) ? sx : [sx]),
      ]}
      {...other}
    >
      {options.map((option) => {
        const selected = value === option.name;

        return (
          <OptionButton
            key={option.name}
            onClick={() => onChangeOption(option.name)}
            sx={{
              height: 64,
              color: option.value,
              ...(selected && {
                bgcolor: hexAlpha(option.value, 0.08),
              }),
            }}
          >
            {icon}
          </OptionButton>
        );
      })}
    </Box>
  );
}
