export interface GoogleDrivePluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  storeRecipes(options: { recipesJson: string, authToken: string }): Promise<{ status: string }>;
}
