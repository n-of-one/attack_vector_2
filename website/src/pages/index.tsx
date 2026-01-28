import { useEffect } from 'react';
import { useHistory } from '@docusaurus/router';

export default function Home() {
    const history = useHistory();

    useEffect(() => {
        history.replace('/attack_vector_2/docs/');
    }, [history]);

    return null;
}