// Payments Domain Model
export class Payment {
  constructor(
    public id: string,
    public description: string,
    public recipientName: string,
    public amount: number,
    public timestamp: string,
    public status: string,
    public paymentType?: string,
    public category?: string,
    public cardId?: string
  ) {}
}
