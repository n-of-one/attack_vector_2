import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
    title: 'Attack Vector',
    tagline: '-',
    favicon: 'img/favicon.svg',

    // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
    future: {
        v4: true, // Improve compatibility with the upcoming Docusaurus v4
    },

    // Set the production url of your site here
    url: 'https://n-of-one.github.io',
    // Set the /<baseUrl>/ pathname under which your site is served
    // For GitHub pages deployment, it is often '/<projectName>/'
    baseUrl: '/attack_vector_2/',

    // GitHub pages deployment config.
    // If you aren't using GitHub pages, you don't need these.
    organizationName: 'n-of-one', // Usually your GitHub org/user name.
    projectName: 'attack_vector_2', // Usually your repo name.

    trailingSlash: true,

    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',

    // Even if you don't use internationalization, you can use this field to set
    // useful metadata like html lang. For example, if your site is Chinese, you
    // may want to replace "en" with "zh-Hans".
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    presets: [
        [
            'classic',
            {
                // Disable the default docs plugin
                docs: false,
                blog: false,
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            },
        ],
    ],

    plugins: [
        [
            '@docusaurus/plugin-content-docs',
            {
                id: 'docs',
                path: 'docs/docs',
                routeBasePath: 'docs',
                sidebarPath: require.resolve('./docs/docs/sidebars.ts'),
                breadcrumbs: false,
            },
        ],

        [
            '@docusaurus/plugin-content-docs',
            {
                id: 'player',
                path: 'docs/player',
                routeBasePath: 'player',
                sidebarPath: require.resolve('./docs/player/sidebars.ts'),
                breadcrumbs: false,
            },
        ],

        [
            '@docusaurus/plugin-content-docs',
            {
                id: 'gm',
                path: 'docs/gm',
                routeBasePath: 'gm',
                sidebarPath: require.resolve('./docs/gm/sidebars.ts'),
                breadcrumbs: false,
            },
        ],

        [
            '@docusaurus/plugin-content-docs',
            {
                id: 'organizer',
                path: 'docs/organizer',
                routeBasePath: 'organizer',
                sidebarPath: require.resolve('./docs/organizer/sidebars.ts'),
                breadcrumbs: false,
            },
        ],

        [
            '@docusaurus/plugin-content-docs',
            {
                id: 'installation',
                path: 'docs/installation',
                routeBasePath: 'installation',
                sidebarPath: require.resolve('./docs/installation/sidebars.ts'),
                breadcrumbs: false,
            },
        ],

    ],

    themeConfig: {
        // Replace with your project's social card
        image: 'img/docusaurus-social-card.jpg',
        colorMode: {
            disableSwitch: true,
            defaultMode: 'dark',
        },
        navbar: {
            title: 'Attack Vector 2',
            logo: {
                alt: 'My Site Logo',
                src: 'img/logo/av2-logo.png',
            },

            items: [
                { to: '/docs', label: '🗎 Docs', position: 'left' },
                { to: '/player', label: '⌨ Player', position: 'left' },
                { to: '/gm', label: '⬣ GM', position: 'left' },
                { to: '/organizer', label: '⛓ Organizer', position: 'left' },
                { to: '/installation', label: '🛠 Installation', position: 'right' },
            ],
        },
        prism: {
            theme: prismThemes.github,
            darkTheme: prismThemes.dracula,
        },
    } satisfies Preset.ThemeConfig,
};

export default config;
