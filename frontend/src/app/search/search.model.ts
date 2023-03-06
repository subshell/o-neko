export type SearchResultEntryType = "PROJECT" | "PROJECT_VERSION";
export interface SearchResultEntry {
  type: SearchResultEntryType;
  text: string;
  id: string;
  projectId: string;
}
