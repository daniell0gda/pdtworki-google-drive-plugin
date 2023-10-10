import { WebPlugin } from '@capacitor/core';

import type { GoogleDrivePluginPlugin } from './definitions';

export class GoogleDrivePluginWeb extends WebPlugin implements GoogleDrivePluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async storeRecipes(options: { recipesJson: string, authToken: string }): Promise<{ status: string }> {
    console.log('STORE RECIPES', options);
    return {status: 'dummy'};
  }

  async fetchRecipes(options: { authToken: string }): Promise<{ recipesJson: string, status: string }> {
    console.log('FETCH RECIPES', options);
    return {status: 'dummy', recipesJson: '{}'};
  }
}
