- Ruben van der Vyver 25007786
- Lucian van der Merwe 25054262
- Matthew Malan 25009801

| Queue Implementation (Cap) | Producers | Consumers | Total Execution Time (s) | Throughput (jobs per second) |
|---|---:|---:|---:|---:|
| BoundedQueue (100) | 4 | 4 | 0.0071 | 1684876 |
| LockFreeQueue | 4 | 4 | 0.0025 | 4825747 |
| BoundedQueue (100) | 6 | 2 | 0.0064 | 1864550 |
| LockFreeQueue | 6 | 2 | 0.0021 | 5800417 |
| BoundedQueue (100) | 2 | 6 | 0.0082 | 1470551 |
| LockFreeQueue | 2 | 6 | 0.0029 | 4085259 |
