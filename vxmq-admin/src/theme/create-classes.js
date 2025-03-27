import { themeConfig } from './theme-config';

// ----------------------------------------------------------------------

export function createClasses(className) {
  return `${themeConfig.classesPrefix}__${className}`;
}
