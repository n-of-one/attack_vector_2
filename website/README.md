# Readme

The documentation for Attack Vector is made with Docusaurus. 


## Local Development

```
npm run start
```


## Deployment to Github pages

```
export GIT_USER=n-of-one
npm run deploy
```

## Packaging as part of a release

First deploy the site to Gitub pages as described above. This will verify that the
documentation is correct. Github pages has more strict requirements for the
files and links than docusaurus when running development locally.

Packaging of the site is done when running the frontend script 'build'.


