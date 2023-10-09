
export interface PodAndContainer {
  podName: string;
  containers: Array<Container>;
}

export interface Container {
  name: string;
  externalLogUrl: string;
}
