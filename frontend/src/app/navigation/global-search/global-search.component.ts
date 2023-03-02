import {Component, ElementRef, Inject, OnInit, Renderer2, ViewChild} from "@angular/core";
import {DOCUMENT} from "@angular/common";

@Component({
  selector: 'on-global-search',
  templateUrl: './global-search.component.html',
  styleUrls: ['./global-search.component.scss']
})
export class GlobalSearchComponent implements OnInit {
  @ViewChild('inputElement') inputElement: ElementRef<HTMLInputElement>;

  constructor(private renderer: Renderer2, @Inject(DOCUMENT) document: Document) {

  }

  ngOnInit(): void {
    this.renderer.listen(document, 'keydown.meta.k', () => {
      this.inputElement.nativeElement.focus();
    });
  }

  clearSearch() {

  }
}
