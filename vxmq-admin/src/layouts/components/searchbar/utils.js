const flattenNavItems = (navItems, parentGroup) => {
  let flattenedItems = [];

  navItems.forEach((navItem) => {
    const currentGroup = parentGroup ? `${parentGroup}-${navItem.title}` : navItem.title;
    const groupArray = currentGroup.split('-');

    flattenedItems.push({
      title: navItem.title,
      path: navItem.path,
      group: groupArray.length > 2 ? `${groupArray[0]}.${groupArray[1]}` : groupArray[0],
    });

    if (navItem.children) {
      flattenedItems = flattenedItems.concat(flattenNavItems(navItem.children, currentGroup));
    }
  });
  return flattenedItems;
};

export function flattenNavSections(navSections) {
  return navSections.flatMap((navSection) =>
    flattenNavItems(navSection.items, navSection.subheader)
  );
}

// ----------------------------------------------------------------------

export function applyFilter({ inputData, query }) {
  if (!query) return inputData;

  return inputData.filter(({ title, path, group }) =>
    [title, path, group].some((field) => field?.toLowerCase().includes(query.toLowerCase()))
  );
}
