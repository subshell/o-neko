export interface MenuEntry {
  title: string;
  href: string;
  icon?: string;
  disabled?: boolean;
}

export interface ExpandableMenuEntry {
  title: string;
  icon: string;
  children: Array<MenuEntry>;
}
