// Portfolio Domain Model
export class PortfolioAsset {
  constructor(
    public id: string,
    public name: string,
    public type: string,
    public value: number,
    public allocation: number
  ) {}
}
