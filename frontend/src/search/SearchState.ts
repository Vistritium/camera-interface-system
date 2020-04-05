import {Data, Image} from "../Model";

export class SearchState {

    selectedPresets: Array<Image>;
    selectedHours: Array<Number>;
    from: Date;
    to: Date;

    constructor(data: Data) {
        this.selectedPresets = [];
        this.selectedHours = [];
        this.from = new Date();
        this.to = new Date();
    }


}