import csv
from collections import defaultdict
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
from matplotlib.backends.backend_pdf import PdfPages


def load_benchmarks(csv_path='report/benchmark_results.csv'):
    data = defaultdict(list)
    with open(csv_path, 'r', newline='') as f:
        for row in csv.DictReader(f):
            key = (row['test_case'], row['scheduler'], row['sweep'])
            data[key].append({
                'q': int(row['q']),
                'cs': int(row['cs']),
                'awt': float(row['awt']),
                'atat': float(row['atat']),
                'art': float(row['art']),
                'cpu': float(row['cpu_util'])
            })
    return data


def generate_graphs(data):
    tcs = ['test_case_1', 'test_case_2', 'test_case_3', 'test_case_4']
    titles_cs = ['Test Case 1 (Q=3)', 'Test Case 2 (Q=3)', 'Test Case 3 - Interactive (Q=3)', 'Test Case 4 - Batch (Q=2)']
    titles_q = ['Test Case 1 (CS=0)', 'Test Case 2 (CS=1)', 'Test Case 3 - Interactive (CS=1)', 'Test Case 4 - Batch (CS=1)']

    fig, axes = plt.subplots(2, 2, figsize=(11, 6.2))
    fig.suptitle('Effect of Context Switching (CS) on AWT & ATAT', fontsize=13, fontweight='bold')
    for ax, tc, title in zip(axes.flat, tcs, titles_cs):
        acs = data[(tc, 'ACS', 'CS')]
        sjf = data[(tc, 'SJF', 'CS')]
        cs = [r['cs'] for r in acs]
        ax.plot(cs, [r['awt'] for r in acs], marker='o', linewidth=2, label='ACS AWT')
        ax.plot(cs, [r['atat'] for r in acs], marker='s', linewidth=2, label='ACS ATAT')
        ax.plot(cs, [r['awt'] for r in sjf], marker='^', linestyle='--', linewidth=1.6, label='SJF AWT')
        ax.plot(cs, [r['atat'] for r in sjf], marker='d', linestyle='--', linewidth=1.6, label='SJF ATAT')
        ax.set_title(title, fontsize=10, fontweight='bold')
        ax.set_xlabel('CS', fontsize=9)
        ax.set_ylabel('Time Units', fontsize=9)
        ax.set_xticks(cs)
        ax.grid(True, linestyle='--', alpha=0.4)
        ax.legend(fontsize=7.5)
    plt.tight_layout(rect=[0, 0, 1, 0.94])
    plt.savefig('report/cs_vs_performance.png', dpi=240)
    plt.close()

    fig, axes = plt.subplots(2, 2, figsize=(11, 6.2))
    fig.suptitle('Effect of Time Quantum (Q) on ACS AWT, ATAT & Response Time', fontsize=13, fontweight='bold')
    for ax, tc, title in zip(axes.flat, tcs, titles_q):
        acs = data[(tc, 'ACS', 'Q')]
        q = [r['q'] for r in acs]
        ax.plot(q, [r['awt'] for r in acs], marker='o', linewidth=2, label='AWT')
        ax.plot(q, [r['atat'] for r in acs], marker='s', linewidth=2, label='ATAT')
        ax.plot(q, [r['art'] for r in acs], marker='^', linestyle=':', linewidth=2, label='Response Time')
        ax.set_title(title, fontsize=10, fontweight='bold')
        ax.set_xlabel('Quantum (Q)', fontsize=9)
        ax.set_ylabel('Time Units', fontsize=9)
        ax.set_xticks(q)
        ax.grid(True, linestyle='--', alpha=0.4)
        ax.legend(fontsize=7.5)
    plt.tight_layout(rect=[0, 0, 1, 0.94])
    plt.savefig('report/q_vs_performance.png', dpi=240)
    plt.close()


def add_header(fig, subtitle):
    fig.text(0.07, 0.95, "Faculty of Computers and Artificial Intelligence — Cairo University", fontsize=9.5, color='#555555')
    fig.text(0.07, 0.928, "CS241: Operating Systems 1  |  Assignment 2: Cloud-Cluster CPU Scheduling", fontsize=9.5, color='#555555')
    fig.text(0.07, 0.885, subtitle, fontsize=15, fontweight='bold', color='#0f172a')


def build_pdf():
    with PdfPages('report/Final_Report.pdf') as pdf:
        fig = plt.figure(figsize=(8.5, 11))
        fig.patch.set_facecolor('white')
        plt.axis('off')
        add_header(fig, "1. Simulation Summary & Test Case Results")

        intro = [
            "We implemented and evaluated two CPU schedulers in Java:",
            "  • Preemptive Shortest-Job First (SJF): Always runs the job with the shortest remaining time (with CS).",
            "  • Adaptive Container Scheduler (ACS): Tier 1 Round Robin (Q), Tier 2 Non-Preemptive Priority",
            "    demotion for CPU-bound batch jobs, and Tier 3 Aging to prevent starvation."
        ]
        y = 0.835
        for line in intro:
            fig.text(0.07, y, line, fontsize=10, color='#1e293b')
            y -= 0.026

        ax_tbl = fig.add_axes([0.07, 0.48, 0.86, 0.22])
        ax_tbl.axis('off')
        cols = ['Test Case', 'Params', 'ACS AWT', 'ACS ATAT', 'ACS ART', 'SJF AWT', 'SJF ATAT', 'SJF ART']
        rows = [
            ['TC1 (Baseline)', 'Q=3, CS=0', '4.33', '8.67', '1.67', '2.00', '6.33', '2.00'],
            ['TC2 (CS + Aging)', 'Q=3, CS=1', '11.00', '16.00', '2.33', '4.00', '9.00', '4.00'],
            ['TC3 (Interactive)', 'Q=3, CS=1', '2.33', '4.33', '2.33', '1.67', '3.67', '1.67'],
            ['TC4 (Heavy Batch)', 'Q=2, CS=1', '13.67', '21.67', '2.00', '7.33', '15.33', '7.33']
        ]
        tbl = ax_tbl.table(cellText=rows, colLabels=cols, loc='center', cellLoc='center')
        tbl.auto_set_font_size(False)
        tbl.set_fontsize(9.2)
        tbl.scale(1, 1.8)
        for (r, c), cell in tbl.get_celld().items():
            cell.set_edgecolor('#cbd5e1')
            if r == 0:
                cell.set_facecolor('#1e293b')
                cell.set_text_props(color='white', fontweight='bold')
            elif r % 2 == 0:
                cell.set_facecolor('#f8fafc')

        notes = [
            "Key Observations:",
            "  • Test Case 1 & Test Case 2 match the assignment specification exactly (TC1: ATAT=8.67, AWT=4.33;",
            "    TC2: ATAT=16.00, AWT=11.00 with Tier 3 Aging promoting P1 at T=11 and T=15).",
            "  • Preemptive SJF achieves lower AWT and ATAT because it always finishes the shortest job first.",
            "  • ACS achieves much faster Response Time on heavy workloads (TC4: ART=2.00 vs. SJF ART=7.33)",
            "    because every new job immediately gets a Round Robin slice in Tier 1."
        ]
        y = 0.42
        for i, line in enumerate(notes):
            fig.text(0.07, y, line, fontsize=10, fontweight='bold' if i == 0 else 'normal', color='#0f172a' if i == 0 else '#334155')
            y -= 0.028

        pdf.savefig(fig)
        plt.close(fig)

        fig = plt.figure(figsize=(8.5, 11))
        fig.patch.set_facecolor('white')
        plt.axis('off')
        add_header(fig, "2. Relation Between CS, Q, Waiting Time & Turnaround Time")

        ax1 = fig.add_axes([0.05, 0.47, 0.90, 0.39])
        ax1.imshow(mpimg.imread('report/cs_vs_performance.png'))
        ax1.axis('off')

        ax2 = fig.add_axes([0.05, 0.06, 0.90, 0.39])
        ax2.imshow(mpimg.imread('report/q_vs_performance.png'))
        ax2.axis('off')

        pdf.savefig(fig)
        plt.close(fig)

        fig = plt.figure(figsize=(8.5, 11))
        fig.patch.set_facecolor('white')
        plt.axis('off')
        add_header(fig, "3. Analysis: CS/Q Effects & ACS Tier 2 Demotion Trade-offs")

        sections = [
            ("Effect of Context Switching (CS) and Quantum (Q)", [
                "• Increasing CS (0 -> 3) increases AWT and ATAT linearly and reduces CPU utilization (in TC2, ACS",
                "  CPU utilization drops from 100% at CS=0 to 50% at CS=3). ACS is more affected by high CS than SJF",
                "  because Round Robin demotions introduce extra process switches.",
                "• Small Quantum (Q=1, 2) gives the best Response Time (TC1: 0.67, TC4: 1.00), but causes frequent",
                "  demotions and higher switching overhead when CS > 0.",
                "• Large Quantum (Q=8, 10) reduces demotions and raises CPU utilization (92.3% in TC4), but degrades",
                "  Response Time (1.00 -> 8.67 in TC4) as Round Robin behaves like FCFS (Convoy Effect)."
            ]),
            ("Trade-offs of ACS Tier 2 Demotion: CPU Utilization vs. Response Time", [
                "• Better CPU Utilization: Pure Round Robin keeps interrupting CPU-heavy batch jobs every Q units,",
                "  wasting CPU cycles on context switches. Demoting long jobs to a Non-Preemptive Priority queue",
                "  (Tier 2) lets them run to completion uninterrupted, maximizing throughput and cache efficiency.",
                "• Better Response Time for Interactive Jobs: Short interactive jobs finish within their Tier 1 slice",
                "  without waiting behind long batch jobs (when Tier 2 is not actively executing).",
                "• The Core Trade-off: Because Tier 2 is strictly non-preemptive, an interactive job arriving while a",
                "  long batch job is already running in Tier 2 must wait until that batch job finishes, causing",
                "  occasional latency spikes in exchange for higher overall CPU utilization."
            ]),
            ("Preventing Starvation with Tier 3 Aging", [
                "• Without aging, low-priority batch jobs in Tier 2 could wait forever behind higher-priority jobs.",
                "• Aging boosts priority over time: Effective Priority = max(1, Initial Priority - floor(Wait / 4)).",
                "• As shown in Test Case 2, P1 (priority 4) ages to priority 2 at T=11 and priority 1 at T=15,",
                "  guaranteeing every batch job finishes within a bounded time."
            ])
        ]

        y = 0.82
        for heading, bullets in sections:
            fig.text(0.07, y, heading, fontsize=12, fontweight='bold', color='#1d4ed8')
            y -= 0.032
            for b in bullets:
                fig.text(0.07, y, b, fontsize=9.8, color='#1e293b')
                y -= 0.025
            y -= 0.025

        pdf.savefig(fig)
        plt.close(fig)

    print("Clean 3-page report built: report/Final_Report.pdf")


if __name__ == '__main__':
    benchmarks = load_benchmarks()
    generate_graphs(benchmarks)
    build_pdf()
