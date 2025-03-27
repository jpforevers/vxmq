import { themeConfig } from 'src/theme/theme-config';

// ----------------------------------------------------------------------

export const SETTINGS_STORAGE_KEY = 'app-settings';

// ----------------------------------------------------------------------

export const defaultSettings = {
  colorScheme: themeConfig.defaultMode,
  direction: themeConfig.direction,
  contrast: 'default',
  navLayout: 'vertical',
  primaryColor: 'default',
  navColor: 'integrate',
  compactLayout: true,
  fontSize: 16,
  fontFamily: themeConfig.fontFamily.primary,
};
