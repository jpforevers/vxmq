export function borderGradient(props) {
  const { color, padding = '2px' } = props ?? {};

  return {
    padding,
    inset: 0,
    width: '100%',
    content: '""',
    height: '100%',
    margin: 'auto',
    position: 'absolute',
    borderRadius: 'inherit',
    /********/
    mask: 'linear-gradient(#FFF 0 0) content-box, linear-gradient(#FFF 0 0)',
    WebkitMask: 'linear-gradient(#FFF 0 0) content-box, linear-gradient(#FFF 0 0)',
    maskComposite: 'exclude',
    WebkitMaskComposite: 'xor',
    ...(color && {
      background: color,
    }),
  };
}
