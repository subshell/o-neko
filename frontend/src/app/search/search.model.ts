export interface ProjectSearchResultEntry {
  name: string;
  id: string;
}

export interface VersionSearchResultEntry {
  name: string;
  id: string;
  projectName: string;
  projectId: string;
}

export interface SearchResult {
  query: string;
  total: number;
  totalProjectsFound: number;
  totalVersionsFound: number;
  projects: Array<ProjectSearchResultEntry>;
  versions: Array<VersionSearchResultEntry>
}
