(request) => {
    const { headers, body } = request;
    delete headers['Content-Length'];
    let responseBody;
    try {
        const parsed = JSON.parse(body);
        const upper = {};
        for (const k of Object.keys(parsed)) {
            upper[k.toUpperCase()] = String(parsed[k]).toUpperCase();
        }
        responseBody = JSON.stringify(upper);
    } catch (e) {
        responseBody = String(body).toUpperCase();
    }
    return { status: '200 OK', headers, body: responseBody };
}
