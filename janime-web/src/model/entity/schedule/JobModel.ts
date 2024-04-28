interface JobModel {
    id?: string,
    name?: string,
    className?: string,
    methodName?: string,
    parameters?: string,
    cronExpression?: string,
    description?: string,
    active?: number,
    type?: number,
    nextExeTime?: string,
    sort?: number
}