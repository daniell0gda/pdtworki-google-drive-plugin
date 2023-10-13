export interface GoogleDrivePluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  storeRecipes(options: { recipesJson: string, authToken: string, appName:string }): Promise<{ status: string }>;
  fetchRecipes(options: { authToken: string, appName:string }): Promise<{ recipesJson: string, status: string }>;
}
