# Finora — Demo Guide

A short walkthrough for presenting the finished application.

## Before the demo

1. Make sure MySQL Server is running and `finora_db` exists.
2. If the tables are empty, run `database/sample_data.sql` in MySQL Workbench.
   Demo rows are marked with `DEMO | ` and can be removed with the cleanup
   statement at the bottom of that file.
3. Launch the app:

   ```bash
   mvn clean javafx:run
   ```

4. The window opens on the **Dasbor** page.

The demo data contains three months of transactions and current-month budgets,
so every chart and table has something to show.

---

## 1. Dashboard — 1 minute

Open **Dasbor** from the sidebar.

Point out:
- Summary cards: **Total Saldo**, **Pemasukan**, **Pengeluaran**.
- The header status shows the active period.
- **Pemasukan vs Pengeluaran** chart on the left.
- **Pengeluaran per Kategori** pie chart on the right.
- **Transaksi Terbaru** table at the bottom.

Try it:
- Pick a narrower **Dari / Sampai** range and press **Terapkan** — every card and chart updates.
- Pick a start date after the end date — a warning appears instead of a broken query.
- Press **Bersihkan** to return to the default range (1 January of the current year → today).

Message: *"Everything on this page is computed from real MySQL records, not hard-coded."*

---

## 2. Transactions — 2 minutes

Open **Transaksi**.

Point out:
- Search box plus Tipe, Kategori, Dari, and Sampai filters.
- Columns: Tanggal, Tipe, Kategori, Jumlah, Deskripsi, Aksi.
- Income amounts are green, expense amounts are orange.

Try it:
- Type `Groceries` in search — the table narrows immediately after **Cari**.
- Filter **Tipe = Pengeluaran**, then choose a category.
- Press **Ubah** on a demo row, change the amount, and save — the table and Dasbor update.
- Press **Hapus** on a demo row and confirm — the row disappears.

Try adding one transaction:
- Press **Tambah Transaksi**, choose a category that matches the type,
  enter an amount greater than zero, and save.
- Submit an empty or zero-amount form to show the validation message.

Message: *"Creating, editing, and deleting a transaction immediately changes every financial summary."*

---

## 3. Budgets — 2 minutes

Open **Anggaran**.

Point out:
- **Bulan** and **Tahun** filters default to the current period.
- Columns: Kategori, Periode, Anggaran, Terpakai, Sisa, Progres, Aksi.
- Terpakai is summed from transactions of the same category and selected month/year.
- Progress bar turns orange when spending exceeds the budget.

Try it:
- Press **Tambah Anggaran**, pick a Pengeluaran category, enter an amount, and save.
- Try to save a second budget for the same category and month — the app explains the duplicate.
- Press **Ubah** to change a limit, then **Hapus** to remove it.

Message: *"Budget progress is live: it follows the transactions recorded for that category."*

---

## 4. Categories — 1 minute

Open **Kategori**.

Point out:
- Separate Pemasukan and Pengeluaran sections.
- Tambah and Ubah actions with name/type validation.

Try it:
- Add a temporary category, then delete it.
- Try to delete a category used by a transaction or budget — deletion is blocked with a clear message.

Message: *"Foreign keys protect financial history from broken references."*

---

## 5. Reports — 1.5 minutes

Open **Laporan**.

Point out:
- Summary cards: Total Pemasukan, Total Pengeluaran, Saldo Bersih, Rasio Tabungan.
- Period status with transaction count.
- Pemasukan vs Pengeluaran Bulanan bar chart.
- Distribusi Pengeluaran pie chart with amount tooltips (hover a slice).
- Perbandingan Bulanan table with Bersih and Rasio Tabungan per month.

Try it:
- Set **Dari** and **Sampai** to cover only one month and press **Terapkan**.
- Press **Bersihkan** to restore the default range.

Message: *"Reports reuse the same aggregate queries as the dashboard, so the numbers always agree."*

---

## 6. Close — 30 seconds

Recap:
- Single local user, no login.
- Three tables only: `categories`, `transactions`, `budgets`.
- JDBC + `PreparedStatement`, `BigDecimal` money, background tasks so the UI never freezes.
- Six phases delivered and the full regression suite passing.

## If something looks empty

Run `database/sample_data.sql` in MySQL Workbench and reopen the affected page
(navigation reloads each page automatically). All demo rows are prefixed with `DEMO | `.
