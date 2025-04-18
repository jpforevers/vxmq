import { useIsClient } from 'minimal-shared/hooks';

import Tabs from '@mui/material/Tabs';
import { styled } from '@mui/material/styles';
import { tabClasses } from '@mui/material/Tab';

// ----------------------------------------------------------------------

const customTabsStyles = {
  root: {
    flexShrink: 0,
    bgcolor: 'background.neutral',
  },
  list: {
    p: 1,
    height: 1,
    gap: { xs: 0 },
  },
  indicator: {
    py: 1,
    height: 1,
    bgcolor: 'transparent',
  },
  tabItem: {
    px: 2,
    zIndex: 1,
    minHeight: 'auto',
  },
};

export function CustomTabs({ children, slotProps, sx, ...other }) {
  const isClient = useIsClient();

  return (
    <Tabs
      sx={[
        customTabsStyles.root,
        {
          [`& .${tabClasses.root}`]: {
            ...customTabsStyles.tabItem,
            ...slotProps?.tab?.sx,
          },
        },
        ...(Array.isArray(sx) ? sx : [sx]),
      ]}
      slotProps={{
        ...slotProps,
        indicator: {
          ...slotProps?.indicator,
          children: isClient && <IndicatorContent sx={slotProps?.indicatorContent?.sx} />,
          sx: [
            customTabsStyles.indicator,
            ...(Array.isArray(slotProps?.indicator?.sx)
              ? slotProps.indicator.sx
              : [slotProps?.indicator?.sx]),
          ],
        },
        list: {
          ...slotProps?.list,
          sx: [
            customTabsStyles.list,
            ...(Array.isArray(slotProps?.list?.sx) ? slotProps.list.sx : [slotProps?.list?.sx]),
          ],
        },
      }}
      {...other}
    >
      {children}
    </Tabs>
  );
}

// ----------------------------------------------------------------------

const IndicatorContent = styled('span')(({ theme }) => ({
  zIndex: 1,
  width: '100%',
  height: '100%',
  display: 'block',
  borderRadius: theme.shape.borderRadius,
  boxShadow: theme.vars.customShadows.z1,
  backgroundColor: theme.vars.palette.common.white,
  ...theme.applyStyles('dark', {
    backgroundColor: theme.vars.palette.grey['900'],
  }),
}));
