// monorepo/ui/components/ui/button.jsx
export function Button({ children, className, ...props }) {
    return (
        <button
            {...props}
            className={`px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-300 ${className}`}
        >
            {children}
        </button>
    );
}
