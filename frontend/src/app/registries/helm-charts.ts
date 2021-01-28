export interface HelmCharts {
  registryId: string;
  charts: {[name: string]: Array<HelmChartVersion>};
}

export interface HelmChartVersion {
  version: string;
  appVersion: string;
  description: string;
}
