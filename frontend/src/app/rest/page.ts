
export interface PageSort {
  unsorted: boolean
  sorted: boolean
  empty: boolean
}

export interface Pageable {
  sort: PageSort
  offset: number
  pageNumber: number
  pageSize: number
}

export interface Page<T> {
  content: Array<T>
  empty: boolean
  first: boolean
  last: boolean
  number: number
  numberOfElements: number
  pageable: Pageable
  size: number
  sort: PageSort
  totalElements: number
  totalPages: number
}
