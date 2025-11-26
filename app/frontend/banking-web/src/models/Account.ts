// Account Domain Model
export class Account {
  constructor(
    public id: string,
    public name: string,
    public balance: number,
    public type: string,
    public status: string
  ) {}
}
