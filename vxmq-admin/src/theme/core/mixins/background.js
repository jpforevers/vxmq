export function bgGradient({ sizes, repeats, images, positions }) {
  return {
    backgroundImage: images?.join(', '),
    backgroundSize: sizes?.join(', ') ?? 'cover',
    backgroundRepeat: repeats?.join(', ') ?? 'no-repeat',
    backgroundPosition: positions?.join(', ') ?? 'center',
  };
}

// ----------------------------------------------------------------------

export function bgBlur({ color, blur = 6, imgUrl }) {
  if (imgUrl) {
    return {
      position: 'relative',
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat',
      backgroundImage: `url(${imgUrl})`,
      '&::before': {
        position: 'absolute',
        top: 0,
        left: 0,
        zIndex: 9,
        content: '""',
        width: '100%',
        height: '100%',
        backdropFilter: `blur(${blur}px)`,
        WebkitBackdropFilter: `blur(${blur}px)`,
        backgroundColor: color,
      },
    };
  }
  return {
    backdropFilter: `blur(${blur}px)`,
    WebkitBackdropFilter: `blur(${blur}px)`,
    backgroundColor: color,
  };
}
