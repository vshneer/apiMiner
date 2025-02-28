// monorepo/ui/components/ui/input.jsx
export function Input({ className, ...props }) {
    return (
        <input
            {...props}
            className={`px-4 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-300 ${className}`}
        />
    );
}
