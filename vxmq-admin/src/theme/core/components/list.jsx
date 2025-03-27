// ----------------------------------------------------------------------

const MuiListItemIcon = {
  /** **************************************
   * STYLE
   *************************************** */
  styleOverrides: {
    root: ({ theme }) => ({ color: 'inherit', minWidth: 'auto', marginRight: theme.spacing(2) }),
  },
};

// ----------------------------------------------------------------------

const MuiListItemAvatar = {
  /** **************************************
   * STYLE
   *************************************** */
  styleOverrides: { root: ({ theme }) => ({ minWidth: 'auto', marginRight: theme.spacing(2) }) },
};

// ----------------------------------------------------------------------

const MuiListItemText = {
  /** **************************************
   * DEFAULT PROPS
   *************************************** */
  defaultProps: {
    slotProps: {
      primary: { typography: 'subtitle2' },
      secondary: { component: 'span' },
    },
  },

  /** **************************************
   * STYLE
   *************************************** */
  styleOverrides: { root: { margin: 0 }, multiline: { margin: 0 } },
};

// ----------------------------------------------------------------------

export const list = { MuiListItemIcon, MuiListItemAvatar, MuiListItemText };
