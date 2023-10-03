# pdtworki-google-drive

Integration with Google Drive

## Install

```bash
npm install pdtworki-google-drive
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`storeRecipes(...)`](#storerecipes)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### storeRecipes(...)

```typescript
storeRecipes(options: { recipesJson: string; authToken: string; }) => Promise<{ status: string; }>
```

| Param         | Type                                                     |
| ------------- | -------------------------------------------------------- |
| **`options`** | <code>{ recipesJson: string; authToken: string; }</code> |

**Returns:** <code>Promise&lt;{ status: string; }&gt;</code>

--------------------

</docgen-api>
