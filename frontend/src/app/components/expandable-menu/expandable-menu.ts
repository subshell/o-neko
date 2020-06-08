export interface MenuEntry {
  title: string;
  href: string;
  icon?: string;
  disabled?: boolean;
  hidden?: boolean;
}

export interface ToplevelMenuEntry {
  title: string;
  icon: string;
  hidden?: boolean;
}

export interface ExpandableMenuEntry extends ToplevelMenuEntry {
  children: Array<MenuEntry>;
}

export interface SingleMenuEntry extends ToplevelMenuEntry {
  isSingleEntry: true;
  href: string;
}
