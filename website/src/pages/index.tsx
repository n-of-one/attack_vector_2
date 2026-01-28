import { useEffect } from 'react';
import { useHistory } from '@docusaurus/router';
import useBaseUrl from '@docusaurus/useBaseUrl';

export default function Home() {
    const history = useHistory();
    const docsUrl = useBaseUrl('/docs/');

    useEffect(() => {
        history.replace(docsUrl);
    }, [history, docsUrl]);

    return null;
}