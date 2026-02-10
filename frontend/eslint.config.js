import js from '@eslint/js'
import globals from 'globals'
import react from 'eslint-plugin-react'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'

export default [
  // 1. GLOBAL IGNORES
  // Don't lint build output or distribution files
  { ignores: ['dist'] },

  // 2. MAIN CONFIGURATION OBJECT
  {
    files: ['**/*.{js,jsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
      parserOptions: {
        ecmaVersion: 'latest',
        ecmaFeatures: { jsx: true },
        sourceType: 'module',
      },
    },
    settings: {
      react: { version: '18.2' }
    },
    plugins: {
      react,
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      // 3. CORE JAVASCRIPT RULES
      ...js.configs.recommended.rules,

      // 4. REACT RECOMMENDED RULES
      ...react.configs.recommended.rules,
      ...react.configs['jsx-runtime'].rules,

      // 5. REACT HOOKS (Critical for preventing bugs)
      ...reactHooks.configs.recommended.rules,

      // 6. CUSTOM OVERRIDES
      // Allow target="_blank" without rel="noreferrer" (modern browsers handle this now)
      'react/jsx-no-target-blank': 'off',

      // Enforce Fast Refresh only on components
      'react-refresh/only-export-components': [
        'warn',
        { allowConstantExport: true },
      ],

      // Prop Types are good, but we don't want to be annoying during rapid prototyping
      // Set to 'warn' or 'off' if you prefer TypeScript-like strictness later
      'react/prop-types': 'off',

      // Prevent unused variables, but allow _vars (standard convention)
      'no-unused-vars': ['warn', { 'argsIgnorePattern': '^_' }],
    },
  },
]