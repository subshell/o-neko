import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Observable, Subscription, interval} from "rxjs";

@Component({
  selector: 'timeout-with-ui',
  templateUrl: './timeout-with-ui.html',
  styleUrls: ['./timeout-with-ui.scss']
})
export class TimeoutWithUiComponent implements OnInit {
  @Input()
  public duration: number;
  @Input()
  public cancelled: Observable<void>;
  public spinnerValue: number = 100;
  @Output()
  private onDone = new EventEmitter<void>();
  private timer: Subscription;

  constructor(private detector: ChangeDetectorRef) {
  }

  ngOnInit() {
    this.timer = interval(this.duration / 100).subscribe(() => {
      this.spinnerValue--;
      if (this.spinnerValue === 0) {
        this.timer.unsubscribe();
      }
      this.detector.detectChanges();
    });
    this.cancelled.subscribe(() => {
      this.timer.unsubscribe();
    });
  }

  public dismiss(): void {
    this.timer.unsubscribe();
    this.onDone.emit();
  }

}
