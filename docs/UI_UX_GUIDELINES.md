# Finora — UI/UX Guidelines

## 1. Design Direction

Finora uses a modern minimalist fintech dashboard design.

Design characteristics:
- Clean and professional.
- Light theme.
- Minimal visual clutter.
- Clear information hierarchy.
- Consistent spacing.
- Rounded cards.
- Subtle borders and shadows.
- Intuitive navigation.

Design inspiration:
- Stripe Dashboard.
- Linear.
- Modern personal finance applications.

These are visual references, not frameworks to be implemented.

## 2. Color Palette

| Element | Color | Usage |
|---|---|---|
| Primary | #16865B | Buttons and active states |
| Background | #F8FAFC | Main background |
| Surface | #FFFFFF | Cards and panels |
| Main Text | #152B25 | Primary text |
| Secondary Text | #64748B | Supporting text |
| Border | #E2E8F0 | Borders and dividers |
| Income | #16A34A | Income indicators |
| Expense | #E07857 | Expense indicators |
| Warning | #F59E0B | Warning indicators |

## 3. Typography

Preferred font: Inter.

Recommended sizes:
- Page title: 24–28px.
- Section title: 16–20px.
- Body text: 13–14px.
- Supporting text: 11–12px.
- Financial values: 24–32px.

Use consistent font weights.

If Inter is unavailable, use a suitable system font.

## 4. Main Layout

Use a consistent application layout.

### Sidebar

Contains:
- Finora logo.
- Dashboard.
- Transactions.
- Budgets.
- Categories.
- Reports.

Requirements:
- Highlight the active page.
- Use icons and labels.
- Maintain consistent spacing.

### Header

Contains:
- Current page title.
- Current date when relevant.
- User profile indicator.

### Main Content

Displays the content of the selected page.

Use consistent padding and spacing.

## 5. Dashboard

Recommended components:
- Total Balance card.
- Income summary card.
- Expense summary card.
- Income versus Expense chart.
- Expense by Category chart.
- Recent Transactions table.

Prioritize important financial information.

## 6. Transactions

Use a clean and readable table.

Columns:
- Date.
- Description.
- Category.
- Type.
- Amount.
- Actions.

Provide:
- Search.
- Category filter.
- Type filter.
- Date filter.
- Add transaction button.
- Edit and delete actions.

Use a simple form dialog for adding and editing transactions.

## 7. Budgets

Display each budget with:
- Category.
- Budget amount.
- Actual spending.
- Remaining amount.
- Progress bar.

Use warning indicators when spending approaches or exceeds the budget.

## 8. Categories

Display category names and types clearly.

Provide actions to:
- Add category.
- Edit category.
- Delete category when safe.

Use appropriate icons.

## 9. Reports

Use JavaFX Charts for:
- Income versus expenses.
- Expense distribution by category.
- Monthly financial comparison.

Charts must use actual database data.

## 10. UI Components

Use consistent styling for:
- Buttons.
- Input fields.
- Dropdowns.
- Date pickers.
- Cards.
- Tables.
- Dialogs.
- Progress bars.

Reusable components may be created when they are useful. Do not create unnecessary component abstractions.

## 11. Interaction States

Provide appropriate:
- Hover states.
- Selected states.
- Disabled states.
- Validation messages.
- Empty states.
- Error messages.

Every interactive button must have a working function.

## 12. JavaFX Guidelines

- Use FXML for page layouts.
- Use JavaFX CSS for styling.
- Use JavaFX Charts for visualizations.
- Keep controllers readable.
- Avoid complex business logic in FXML controllers.
- Use background tasks for database operations that could block the interface.
- Ensure the layout works at different window sizes.

## 13. Design Restrictions

Avoid:
- Default-looking JavaFX interfaces.
- Excessive gradients.
- Excessive shadows.
- Too many colors.
- Unnecessary animations.
- Emoji as primary icons.
- Crowded layouts.
- Inconsistent spacing.
- Decorative buttons without functionality.

The final application should feel like a cohesive, modern desktop fintech product.