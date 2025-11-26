// Dashboard Domain Model
export class DashboardSummary {
  constructor(
    public totalBalance: number,
    public totalCreditLimit: number,
    public utilizationRate: number
  ) {}
}
